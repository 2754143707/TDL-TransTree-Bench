Message J5_4_to_Link11_Translation_Tree Translation Trees {
    ACTION {
        A. "FWD M.4D/M.84D INITIAL SEQUENCE."
        B. "FWD M.4D/M.84D/M.4B INITIAL SEQUENCE."
        C. "FWD M.4D/M.84D SEQUENCE."
        D. "FWD M.4D/M.84D/M.4B SEQUENCE."
        E. "GO TO TEST NODE 4."
        F. "FWD APPROPRIATE INITIAL SEQUENCE."
        G. "FWD M.9A(AC=0, SI=1)."
        H. "FWD M.9A(AC=5)."
        I. "FWD M.9A(AC=7)."
        J. "RETAIN ALERT STATUS (ON/OFF) IN FJU DATA BASE."
        K. "GO TO TEST NODE 5."
        Z. "END TRANSLATION."


}
CONDITION {
    Node1: "THIS IS INITIAL REPORT FROM THE DATA SOURCE."
    Node2: "SPECIAL PROCESSING INDICATOR HAS CHANGED."
    Node3: "MINUTE OR HOUR OTHER THAN NO STATEMENT."
    Node4: "SIMULATION INDICATOR = 1."
    Node5: "FORCE TELL INDICATOR HAS CHANGED."
    Node6: "EMERGENCY INDICATOR HAS CHANGED."
    Node7: "EMERGENCY INDICATOR = 1."
    Node8: "FORCE TELL INDICATOR = 1."
}
IF (Node1) {
    IF (Node2) {
        IF (Node3) {
            EXECUTE(A, Z)


    }
    ELSE {
        EXECUTE(B, Z)


}
}
ELSE {
    IF (Node3) {
        EXECUTE(C, Z)


}
ELSE {
    EXECUTE(D, Z)
}
}
}
ELSE {
    EXECUTE(E)
    IF (Node4) {
        EXECUTE(G, Z)


}
ELSE {
    IF (Node5) {
        IF (Node6) {
            IF (Node7) {
                EXECUTE(I, Z)


        }
        ELSE {
            IF (Node8) {
                EXECUTE(J, Z)


        }
        ELSE {
            EXECUTE(H, Z)


    }
}
}
ELSE {
    IF (Node7) {
        EXECUTE(I, Z)


}
ELSE {
    IF (Node8) {
        EXECUTE(J, Z)


}
ELSE {
    EXECUTE(H, Z)
}
}
}
}
ELSE {
    EXECUTE(K)
    IF (Node6) {
        IF (Node7) {
            EXECUTE(I, Z)


    }
    ELSE {
        IF (Node8) {
            EXECUTE(J, Z)


    }
    ELSE {
        EXECUTE(H, Z)


}
}
}
ELSE {
    IF (Node7) {
        EXECUTE(I, Z)


}
ELSE {
    IF (Node8) {
        EXECUTE(J, Z)


}
ELSE {
    EXECUTE(H, Z)
}
}
}
}
}
}
}
