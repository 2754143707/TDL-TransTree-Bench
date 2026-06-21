Message J7_0_to_Link_11_11B Translation Trees {
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
    1: "ACTION, TRACK MANAGEMENT = 5, 6, OR 7."
    2: "ACTION, TRACK MANAGEMENT = 0."
    3: "IS TN, REFERENCE AN ACTIVE UNIT."
    4: "IS TN, SOURCE THE CURRENT R2 FOR THE TN, REFERENCE."
    5: "ACTION, TRACK MANAGEMENT = 1."
    6: "ACTION, TRACK MANAGEMENT = 2."
    7: "ENVIRONMENT/CATEGORY = 2 OR 3."
    8: "CONTROLLING UNIT INDICATOR = 1."
    9: "ACTION, TRACK MANAGEMENT = 3."
    10: "ALERT STATUS CHANGE = 1."
}
IF (1) {
    EXECUTE(A, Z)
}
ELSE {
    IF (2) {
        IF (3) {
            EXECUTE(B, C, Z)


    }
    ELSE {
        IF (4) {
            EXECUTE(B, Z)


    }
    ELSE {
        EXECUTE(A, Z)


}
}
}
ELSE {
    IF (5) {
        IF (7) {
            IF (8) {
                EXECUTE(D, L, Z)


        }
        ELSE {
            EXECUTE(Z)


    }
}
ELSE {
    EXECUTE(A, Z)
}
}
ELSE {
    IF (6) {
        IF (7) {
            EXECUTE(E, Z)


    }
    ELSE {
        EXECUTE(A, Z)


}
}
ELSE {
    IF (9) {
        IF (10) {
            EXECUTE(F, G, Z)


    }
    ELSE {
        EXECUTE(H, I, Z)


}
}
ELSE {
    IF (10) {
        EXECUTE(F, J, Z)


}
ELSE {
    EXECUTE(H, K, Z)
}
}
}
}
}
}
}