import os
import re
import collections
import math
import sys
import difflib

# --- Configuration ---
PROJECT_ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
DATASET_GT_DIR = os.path.join(PROJECT_ROOT, "dataset", "ground_truth")
OUTPUT_ROOT = os.path.join(PROJECT_ROOT, "results")

# Run Configurations
RUNS = {
    "1_baseline": "1_baseline_raw",
    "2_color": "2_ablation_color",
    "3_anchor": "3_ablation_anchor",
    "4_vcot_k0": "4_vcot_k0",
    "5_proposed_k3": "5_proposed_k3"
}

# Model Names
MODELS = [
    "qwen-plus",
    "gemini",
    "claude" # Uncomment when data is ready
]

# --- Parsing Logic ---

class Node:
    def __init__(self, label, children=None):
        self.label = label
        self.children = children if children is not None else []

    def __repr__(self):
        return f"Node({self.label[:20]}..., {len(self.children)} ch)"

class DSLParser:
    def __init__(self, content):
        self.content = content
        self.actions = {}
        self.conditions = {}
        self.tree_root = None
    
    def parse(self):
        # 1. Extract ACTION block
        action_match = re.search(r'ACTION\s*\{(.*?)\}', self.content, re.DOTALL | re.IGNORECASE)
        if action_match:
            action_text = action_match.group(1)
            for line in action_text.split('\n'):
                line = line.strip()
                if not line: continue
                m = re.match(r'^([A-Z0-9]+)\.\s*(.*)', line)
                if m:
                    key = m.group(1).upper()
                    val = m.group(2).strip().strip('"').strip("'")
                    self.actions[key] = val

        # 2. Extract CONDITION block
        cond_match = re.search(r'CONDITION\s*\{(.*?)\}', self.content, re.DOTALL | re.IGNORECASE)
        if cond_match:
            cond_text = cond_match.group(1)
            for line in cond_text.split('\n'):
                line = line.strip()
                if not line: continue
                m = re.match(r'^([A-Za-z0-9_]+):\s*(.*)', line)
                if m:
                    key = m.group(1)
                    val = m.group(2).strip().strip('"').strip("'")
                    self.conditions[key] = val

        # 3. Extract LogicBlock
        logic_text = self._extract_block("LogicBlock")
        if not logic_text:
             logic_text = self._extract_block("Logic Block")
        
        if not logic_text and cond_match:
            # Fallback
            rest = self.content[cond_match.end():].strip()
            if "IF" in rest or "if" in rest:
                logic_text = rest

        if logic_text:
            self.tree_root = self._parse_logic_recursive(logic_text)

        return self

    def _extract_block(self, keyword):
        start_regex = re.compile(re.escape(keyword) + r'\s*\{', re.IGNORECASE)
        match = start_regex.search(self.content)
        if not match: return None
        
        start_idx = match.end()
        count = 1
        i = start_idx
        while i < len(self.content) and count > 0:
            c = self.content[i]
            if c == '{': count += 1
            elif c == '}': count -= 1
            i += 1
        return self.content[start_idx : i-1]

    def _parse_logic_recursive(self, text):
        text = text.strip()
        if not text: return None
        
        # 1. IF (...)
        # Using a loop to handle nested structures correctly without regex for the whole block
        if text.upper().startswith("IF"):
            # Find condition parens
            p_start = text.find('(')
            if p_start != -1:
                # find matching paren
                p_count = 1
                p_end = p_start + 1
                while p_end < len(text) and p_count > 0:
                    if text[p_end] == '(': p_count += 1
                    elif text[p_end] == ')': p_count -= 1
                    p_end += 1
                
                if p_count == 0:
                    cond_ref = text[p_start+1 : p_end-1].strip()
                    # Resolve condition
                    cond_content = self.conditions.get(cond_ref, cond_ref)
                    # Try loose match
                    if cond_ref not in self.conditions:
                        best_match = None
                        for k in self.conditions:
                            if k.lower() == cond_ref.lower(): best_match = self.conditions[k]
                        if best_match: cond_content = best_match

                    cond_content = cond_content.strip('"').strip("'")
                    
                    # Find { ... }
                    remainder = text[p_end:].strip()
                    if remainder.startswith('{'):
                         then_block, rest = self._extract_brace_content(remainder)
                         then_node = self._parse_logic_recursive(then_block)
                         children = []
                         if then_node: children.append(then_node)
                         
                         rest = rest.strip()
                         # ELSE
                         if rest.upper().startswith("ELSE"):
                             rest = rest[4:].strip()
                             if rest.startswith('{'):
                                 else_block, _ = self._extract_brace_content(rest)
                                 else_node = self._parse_logic_recursive(else_block)
                                 if else_node: children.append(else_node)
                             elif rest.upper().startswith("IF"): # ELSE IF
                                 else_node = self._parse_logic_recursive(rest)
                                 if else_node: children.append(else_node)
                                 
                         return Node(cond_content, children)

        # 2. EXECUTE
        if "EXECUTE" in text.upper():
            # Extract content in ()
            start = text.find('(')
            end = text.rfind(')')
            if start != -1 and end != -1:
                inner = text[start+1:end]
                refs = [r.strip() for r in inner.split(',')]
                resolved = []
                for r in refs:
                    # Resolve Action Ref r (e.g. "A" or "A.")
                    key = r.replace(".", "")
                    val = self.actions.get(key)
                    if not val:
                        # Case insensitive check
                        for k, v in self.actions.items():
                            if k.lower() == key.lower():
                                val = v; break
                    if not val: val = r
                    resolved.append(val.strip('"').strip("'"))
                return Node("EXECUTE: " + "; ".join(resolved), [])

        return None

    def _extract_brace_content(self, text):
        if not text.startswith('{'): return "", text
        count = 1
        i = 1
        while i < len(text) and count > 0:
            if text[i] == '{': count += 1
            elif text[i] == '}': count -= 1
            i += 1
        return text[1 : i-1], text[i:]

# --- Metrics ---

class TreeEditDistance:
    def __init__(self):
        self.memo = {}

    def calculate_normalized_distance(self, t1, t2):
        s1 = self._size(t1)
        s2 = self._size(t2)
        if s1 == 0 and s2 == 0: return 1.0 # Both empty -> perfect match
        if s1 == 0: return 0.0 # One empty -> 0 similarity
        if s2 == 0: return 0.0
        
        dist = self._ted(t1, t2)
        
        # Paper Formula: NTED = 1 - D(T_gen, T_gt) / max(|T_gen|, |T_gt|)
        # This represents "Structural Similarity", closer to 1 is better.
        max_nodes = max(s1, s2)
        similarity = 1.0 - (dist / max_nodes)
        
        return max(0.0, similarity) # Ensure non-negative 

    def _ted(self, t1, t2):
        if t1 is None and t2 is None: return 0
        if t1 is None: return self._size(t2)
        if t2 is None: return self._size(t1)
        
        key = (id(t1), id(t2))
        if key in self.memo: return self.memo[key]
        
        # Node Match Cost
        cost_match = 0 if self._soft_match(t1.label, t2.label) else 1
        
        # Simple recursion for children (Note: true TED is O(N^3), this is simplified alignment)
        # But for strictly ordered trees (like code blocks), alignment is appropriate.
        # We align child i with child i (or with gaps).
        # Need Levenshtein on children sequence.
        
        children1 = t1.children
        children2 = t2.children
        m = len(children1)
        n = len(children2)
        
        dp = [[0] * (n + 1) for _ in range(m + 1)]
        
        for i in range(1, m + 1):
            dp[i][0] = dp[i-1][0] + self._size(children1[i-1])
        for j in range(1, n + 1):
            dp[0][j] = dp[0][j-1] + self._size(children2[j-1])
            
        for i in range(1, m + 1):
            for j in range(1, n + 1):
                c1 = children1[i-1]
                c2 = children2[j-1]
                cost_sub = self._ted(c1, c2)
                
                dp[i][j] = min(
                    dp[i-1][j] + self._size(c1), # Delete c1
                    dp[i][j-1] + self._size(c2), # Insert c2
                    dp[i-1][j-1] + cost_sub      # Substitute/Match
                )
        
        res = cost_match + dp[m][n]
        self.memo[key] = res
        return res

    def _soft_match(self, s1, s2):
        # Allow slight differences in strings
        return difflib.SequenceMatcher(None, s1.lower(), s2.lower()).ratio() > 0.8

    def _size(self, t):
        if t is None: return 0
        s = 1
        for c in t.children:
            s += self._size(c)
        return s

def extract_actions_list(node, lst):
    if node is None: return
    if "EXECUTE" in node.label:
        content = node.label.replace("EXECUTE:", "")
        parts = content.split(";")
        for p in parts:
            lst.append(p.strip())
    for c in node.children:
        extract_actions_list(c, lst)

def calculate_soft_f1(t_gen, t_gt):
    gen_acts = []
    gt_acts = []
    extract_actions_list(t_gen, gen_acts)
    extract_actions_list(t_gt, gt_acts)
    
    # Normalize for strict check
    def norm(s): return re.sub(r'[^a-zA-Z0-9]', '', s.lower())
    
    gen_norm = [norm(s) for s in gen_acts]
    gt_norm = [norm(s) for s in gt_acts]
    
    if not gt_norm: return 0.0
    if not gen_norm: return 0.0

    # Strict Set F1 (ignoring order and duplicates for now? Or multiset?)
    # Users usually expect Set F1 for extraction.
    # But if order matters (Sequence), we should punish out of order.
    # Let's use Multiset Intersection.
    
    c_gen = collections.Counter(gen_norm)
    c_gt = collections.Counter(gt_norm)
    
    tp = sum((c_gen & c_gt).values())
    fp = sum((c_gen - c_gt).values())
    fn = sum((c_gt - c_gen).values())
    
    if tp == 0: return 0.0
    
    prec = tp / (tp + fp)
    rec = tp / (tp + fn)
    
    return 2 * prec * rec / (prec + rec)

def count_errors(err_path):
    if not os.path.exists(err_path): return 0
    try:
        c = open(err_path, 'r', encoding='utf-8').read()
        m = re.search(r'错误数量:\s*(\d+)', c)
        if m: return int(m.group(1))
        # Fallback check
        if "Failure" in c or "错误" in c or "Error" in c: return 1
        return 0
    except: return 0

# --- Main ---
def run():
    print("Loading Ground Truth...")
    gt_map = {}
    if os.path.exists(DATASET_GT_DIR):
        for f in os.listdir(DATASET_GT_DIR):
            if f.endswith(".dsl"):
                gt_map[f.replace(".dsl","").replace("_",".")] = open(os.path.join(DATASET_GT_DIR, f), 'r', encoding='utf-8').read()
                # Also handle raw names if needed, but we normalized keys in map
                gt_map[f.replace(".dsl","")] = gt_map[f.replace(".dsl","").replace("_",".")]

    ted_calc = TreeEditDistance()

    for model_name in MODELS:
        print(f"\n{'='*30}")
        print(f"📊 Model: {model_name.upper()}")
        print(f"{'='*30}")
        print(f"{'Run Mode':<20} | {'Pass@1 (%)':<12} | {'NTED (High=Good)':<16} | {'F1 (High=Good)':<15} | {'CFR (%)':<10}")
        print("-" * 90)

        for run_key in sorted(RUNS.keys()):
            run_folder = RUNS[run_key]
            run_path = os.path.join(OUTPUT_ROOT, run_folder, model_name)
            
            stat = {'tot':0, 'p1':0, 'fix':0, 'nted_s':0, 'nted_n':0, 'f1_s':0, 'f1_n':0}
            
            if not os.path.exists(run_path): 
                # print(f"{run_key:<20} | (No Output Found)")
                continue
            
            files = [f for f in os.listdir(run_path) if f.endswith(".dsl") and "attempt-1.dsl" in f]
            
            for f in files:
                stat['tot'] += 1
                base = f.replace("-attempt-1.dsl", "")
                
                # Syntax Pass
                err_file = os.path.join(run_path, f.replace(".dsl", "-errors.txt"))
                passed = (count_errors(err_file) == 0)
                if passed: stat['p1'] += 1
                
                # Repair
                final_pass = passed
                best_file = os.path.join(run_path, f)
                
                is_k3 = "k3" in run_key
                
                # CFR Definition: Fixed within 3 attempts (Total 4 including initial)
                # Paper says k<=3 iterations, meaning initial + 3 fixes = 4 total attempts.
                # Code loop range(2, max_r+2) covers attempts 2, 3, 4 ... up to max_retries+1.
                # If max_retries=4 in Java code (4 total attempts), loop is correct.
                max_r = 3 if is_k3 else 0 
                
                if not passed and max_r > 0:
                    # First pass: try to find a PASSING one
                    found_pass = False
                    for i in range(2, max_r + 2): # Check attempt-2, -3, -4
                        p = os.path.join(run_path, base + f"-attempt-{i}.dsl")
                        if os.path.exists(p):
                            if count_errors(p.replace(".dsl", "-errors.txt")) == 0:
                                best_file = p
                                final_pass = True
                                found_pass = True
                                break
                    
                    # If no passing file found, stick with attempt-1 or try last existing?
                    # Usually last attempt in a repair loop is the most refined, even if still failing.
                    if not found_pass:
                        for i in range(max_r+1, 1, -1):
                            p = os.path.join(run_path, base + f"-attempt-{i}.dsl")
                            if os.path.exists(p):
                                 best_file = p
                                 break
                
                if final_pass and not passed: stat['fix'] += 1
                
                # Metrics
                clean_name = base.replace("_after", "")
                # Heuristic match
                gt_key = None
                # keys in gt_map are like "J3.0", "J10.6"
                # clean_name like "J3_0_1"
                
                # Try 1: Exact
                if clean_name in gt_map: gt_key = clean_name
                
                # Try 2: Replace _ with .
                if not gt_key:
                    cand = clean_name.replace("_", ".")
                    if cand in gt_map: gt_key = cand
                    
                # Try 3: Prefix match (J3.0 matches J3.0_1)
                if not gt_key:
                    for k in gt_map:
                        if clean_name.replace("_", ".").startswith(k):
                            gt_key = k
                            break
                
                if gt_key:
                    try:
                        # NTED
                        # Calculate on tree of best_attempt
                        # Wait, if we are evaluating "Pass@1", we should use attempt 1 for comparison?
                        # No, valid comparison is usually Final Output vs Ground Truth.
                        # But for "Pass@1" metric we use attempt 1.
                        # For NTED/F1, we should use the "Best Available" output.
                        
                        # Correction: If run has repairs (k3), we want to show the quality of the REDEEMED code?
                        # Or the quality of the first attempt?
                        # Usually "Performance of Method" means final output.
                        
                        c_gen = open(best_file, 'r', encoding='utf-8').read()
                        t_gen = DSLParser(c_gen).parse().tree_root
                        t_gt = DSLParser(gt_map[gt_key]).parse().tree_root
                        
                        if t_gt and t_gen:
                            nted = ted_calc.calculate_normalized_distance(t_gen, t_gt)
                            f1 = calculate_soft_f1(t_gen, t_gt)
                            
                            stat['nted_s'] += nted
                            stat['nted_n'] += 1
                            stat['f1_s'] += f1
                            stat['f1_n'] += 1
                    except: pass
            
            if stat['tot'] == 0: continue
            
            pass_rate = stat['p1'] / stat['tot'] * 100
            nted = stat['nted_s'] / stat['nted_n'] if stat['nted_n'] else 0
            f1 = stat['f1_s'] / stat['f1_n'] if stat['f1_n'] else 0
            fail = stat['tot'] - stat['p1']
            cfr = (stat['fix'] / fail * 100) if fail > 0 else 0
            cfr_s = f"{cfr:.2f}" if is_k3 else "-"
            
            print(f"{run_key:<20} | {pass_rate:6.2f}%      | {nted:6.4f}          | {f1:6.4f}          | {cfr_s:<10}")

        print("-" * 90)

if __name__ == "__main__":
    run()
