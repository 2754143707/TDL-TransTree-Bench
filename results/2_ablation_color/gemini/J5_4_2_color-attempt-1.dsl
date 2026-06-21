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
    Node4: "SIMULATION INDICATOR = 1."
    Node5: "FORCE TELL INDICATOR HAS CHANGED."
    Node6: "EMERGENCY INDICATOR HAS CHANGED."
    Node7: "EMERGENCY INDICATOR = 1."
    Node8: "FORCE TELL INDICATOR = 1."
}
IF (Node4) {
    EXECUTE(F, G, H, J)
    IF (Node5) {
        IF (Node6) {
            IF (Node7) {
                IF (Node8) {
                    EXECUTE(F, G, H, J, K)


            }
            ELSE {
                EXECUTE(F, H, I, J, Z)


        }


}
ELSE {
    IF (Node8) {
        EXECUTE(F, H, I, J, Z)


}
ELSE {
    EXECUTE(F, H, J, Z)
}
}
}
ELSE {
    IF (Node8) {
        EXECUTE(F, H, I, J, Z)


}
ELSE {
    EXECUTE(F, H, J, Z)
}
}
}
ELSE {
    IF (Node6) {
        IF (Node7) {
            EXECUTE(F, H, I, J, Z)


    }
    ELSE {
        EXECUTE(F, H, J, Z)


}
}
ELSE {
    EXECUTE(F, H, J, Z)
}
}
}
ELSE {
    IF (Node5) {
        IF (Node6) {
            IF (Node7) {
                IF (Node8) {
                    EXECUTE(F, G, H, J, K)


            }
            ELSE {
                EXECUTE(F, H, I, J, Z)


        }


}
ELSE {
    IF (Node8) {
        EXECUTE(F, H, I, J, Z)


}
ELSE {
    EXECUTE(F, H, J, Z)
}
}
}
ELSE {
    IF (Node8) {
        EXECUTE(F, H, I, J, Z)


}
ELSE {
    EXECUTE(F, H, J, Z)
}
}
}
ELSE {
    IF (Node6) {
        IF (Node7) {
            EXECUTE(F, H, I, J, Z)


    }
    ELSE {
        EXECUTE(F, H, J, Z)


}
}
ELSE {
    EXECUTE(F, H, J, Z)
}
}
}
}
