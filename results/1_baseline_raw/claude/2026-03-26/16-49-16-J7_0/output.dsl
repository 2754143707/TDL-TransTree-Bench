Message J7.0 Translation Trees {
    ACTION {
        A. "DISCARD MESSAGE."
        B. "FWD M.9A(AC=4)."
        C. "DECLARE UNIT INACTIVE IN FJU DATABASE."
        D. "FWD M.9A(AC=1)."
        E. "FWD M.9A(AC=2)."
        F. "FWD M.9A(AC=5)."
        G. "SET EMERGENCY INDICATOR IN DATABASE."
        H. "FWD M.9A(AC=7)."
        I. "CLEAR EMERGENCY INDICATOR IN DATABASE."
        J. "SET FORCE TELL INDICATOR IN DATABASE."
        K. "CLEAR FORCE TELL INDICATOR IN DATABASE."
        L. "FWD M.9A(AC=6)."
        Z. "END TRANSLATION."


}
CONDITION {
    Node1: "ACTION, TRACK MANAGEMENT = 5, 6, OR 7."
    Node2: "ACTION, TRACK MANAGEMENT = 0."
    Node3: "IS TN, REFERENCE AN ACTIVE UNIT."
    Node4: "IS TN, SOURCE THE CURRENT R2 FOR THE TN, REFERENCE."
    Node5: "ACTION, TRACK MANAGEMENT = 1."
    Node6: "ACTION, TRACK MANAGEMENT = 2."
    Node7: "ENVIRONMENT/CATEGORY = 2 OR 3."
    Node8: "CONTROLLING UNIT INDICATOR = 1."
    Node9: "ACTION, TRACK MANAGEMENT = 3."
    Node10: "ALERT STATUS CHANGE = 1."
}
IF (Node1) {
    EXECUTE(A, Z)
}
ELSE {
    IF (Node2) {
        IF (Node3) {
            IF (Node4) {
                EXECUTE(A, B, C, Z)


        }
        ELSE {
            EXECUTE(A, B, Z)


    }
}
ELSE {
    EXECUTE(A, Z)
}
}
ELSE {
    IF (Node5) {
        IF (Node7) {
            IF (Node8) {
                EXECUTE(A, D, L, Z)


        }
        ELSE {
            EXECUTE(A, D, Z)


    }
}
ELSE {
    EXECUTE(A, Z)
}
}
ELSE {
    IF (Node6) {
        IF (Node7) {
            EXECUTE(A, E, Z)


    }
    ELSE {
        EXECUTE(A, Z)


}
}
ELSE {
    IF (Node9) {
        IF (Node10) {
            EXECUTE(A, F, G, H, I, J, Z)


    }
    ELSE {
        EXECUTE(A, F, H, Z)


}
}
ELSE {
    IF (Node10) {
        EXECUTE(A, F, G, H, I, K, Z)


}
ELSE {
    EXECUTE(A, F, H, Z)
}
}
}
}
}
}
}