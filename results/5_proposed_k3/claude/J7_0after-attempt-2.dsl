Message M Translation Trees {
ACTION {
A. Action A
B. Action B
C. Action C
D. Action D
E. Action E
F. Action F
G. Action G
H. Action H
I. Action I
J. Action J
K. Action K
L. Action L
Z. Action Z
}
CONDITION {
C1: Test Node 1 Condition
C2: Test Node 2 Condition
C3: Test Node 3 Condition
C4: Test Node 4 Condition
C5: Test Node 5 Condition
C6: Test Node 6 Condition
C7: Test Node 7 Condition
C8: Test Node 8 Condition
C9: Test Node 9 Condition
C10: Test Node 10 Condition
}
IF (C1) {
IF (C2) {
IF (C3) {
EXECUTE(A, Z)
} ELSE {
IF (C4) {
EXECUTE(B, C, Z)
} ELSE {
EXECUTE(B, Z)
}
}
} ELSE {
IF (C5) {
IF (C7) {
IF (C8) {
EXECUTE(A, L, Z)
} ELSE {
EXECUTE(D, Z)
}
} ELSE {
EXECUTE(A, E, Z)
}
} ELSE {
IF (C6) {
IF (C7) {
IF (C9) {
IF (C10) {
EXECUTE(B, F, G, H, I, Z)
} ELSE {
EXECUTE(B, F, G, Z)
}
} ELSE {
EXECUTE(B, Z)
}
} ELSE {
IF (C9) {
IF (C10) {
EXECUTE(F, J, Z)
} ELSE {
EXECUTE(F, Z)
}
} ELSE {
EXECUTE(H, K, Z)
}
}
}
}
} ELSE {
EXECUTE(A, Z)
}
}
