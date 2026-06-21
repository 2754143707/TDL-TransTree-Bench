Message J3_0_Node26 Translation Trees {
    ACTION {
        A. "DISCARD MESSAGE."
        K. "FWD M.4A/M.84A."
        M. "FWD M.4B."
        S. "COMBINE DATA WITH PRECEDING J3.0 HAVING REMAINING TIME DATA."
        Z. "END TRANSLATION."


}
CONDITION {
    Node26: "TIME FUNCTION = 2."
}
IF (Node26) {
    EXECUTE (K, M, S, Z)
}
ELSE {
    EXECUTE (A, Z)
}
}
