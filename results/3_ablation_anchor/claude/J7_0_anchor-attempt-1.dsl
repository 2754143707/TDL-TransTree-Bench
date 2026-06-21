Message J7_0 Translation Trees {
    ACTION {
        A. DISCARD MESSAGE
        B. FWD M.9A(AC=4)
        C. DECLARE UNIT INACTIVE IN FJU DATABASE
        D. FWD M.9A(AC=1)
        E. FWD M.9A(AC=2)
        F. FWD M.9A(AC=5)
        G. SET EMERGENCY INDICATOR IN DATABASE
        H. FWD M.9A(AC=7)
        I. CLEAR EMERGENCY INDICATOR IN DATABASE
        J. SET FORCE TELL INDICATOR IN DATABASE
        K. CLEAR FORCE TELL INDICATOR IN DATABASE
        L. FWD M.9A(AC=6)
        Z. END TRANSLATION


}
CONDITION {
    N1: ACTION, TRACK MANAGEMENT = 5, 6, OR 7
    N2: ACTION, TRACK MANAGEMENT = 0
    N3: IS TN, REFERENCE AN ACTIVE UNIT
    N4: IS TN, SOURCE THE CURRENT R2 FOR THE TN, REFERENCE
    N5: ACTION, TRACK MANAGEMENT = 1
    N6: ACTION, TRACK MANAGEMENT = 2
    N7: ENVIRONMENT/CATEGORY = 2 OR 3
    N8: CONTROLLING UNIT INDICATOR = 1
    N9: ACTION, TRACK MANAGEMENT = 3
    N10: ALERT STATUS CHANGE = 1
}
IF (N1) {
    EXECUTE(A, Z)
}
ELSE {
    IF (N2) {
        IF (N3) {
            IF (N4) {
                EXECUTE(B, C, Z)


        }
        ELSE {
            EXECUTE(B, Z)


    }
}
ELSE {
    EXECUTE(A, Z)
}
}
ELSE {
    IF (N5) {
        IF (N7) {
            IF (N8) {
                EXECUTE(D, L, Z)


        }
        ELSE {
            EXECUTE(D, Z)


    }
}
ELSE {
    EXECUTE(A, Z)
}
}
ELSE {
    IF (N6) {
        IF (N7) {
            EXECUTE(E, Z)


    }
    ELSE {
        EXECUTE(A, Z)


}
}
ELSE {
    IF (N9) {
        IF (N10) {
            EXECUTE(F, G, H, I, Z)


    }
    ELSE {
        EXECUTE(F, G, Z)


}
}
ELSE {
    IF (N10) {
        EXECUTE(F, J, H, K, Z)


}
ELSE {
    EXECUTE(F, H, Z)
}
}
}
}
}
}
}
