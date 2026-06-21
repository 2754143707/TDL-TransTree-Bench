Message J5_4_to_Link_11 Translation Trees {
    ACTION {
        F. "FWD APPROPRIATE INITIAL SEQUENCE."
        G. "FWD M.9A(AC=0, SI=1)."
        H. "FWD M.9A(AC=5)."
        I. "FWD M.9A(AC=7)."
        J. "RETAIN ALERT STATUS (ON/OFF) IN FJU DATA BASE."
        K. "GO TO TEST NODE 5."
        Z. "END TRANSLATION."


}
CONDITION {
    4: "SIMULATION INDICATOR = 1."
    5: "FORCE TELL INDICATOR HAS CHANGED."
    6: "EMERGENCY INDICATOR HAS CHANGED."
    7: "EMERGENCY INDICATOR = 1."
    8: "FORCE TELL INDICATOR = 1."
}
IF (4) {
    EXECUTE(G, K)
}
ELSE {
    IF (5) {
        IF (6) {
            IF (7) {
                IF (8) {
                    EXECUTE(F, H, J, Z)


            }
            ELSE {
                EXECUTE(F, H, I, J, Z)


        }


}
ELSE {
    EXECUTE(F, H, I, J, Z)
}
}
ELSE {
    IF (8) {
        EXECUTE(I, J, Z)


}
ELSE {
    EXECUTE(F, H, J, Z)
}
}
}
ELSE {
    IF (6) {
        IF (7) {
            EXECUTE(I, J, Z)


    }
    ELSE {
        EXECUTE(F, H, J, Z)


}
}
ELSE {
    EXECUTE(I, J, Z)
}
}
}
}