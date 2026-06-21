Message M9A Translation Trees {
    ACTION {
        A. "DISCARD MESSAGE."
        B. "FWD J7.1."
        C. "RETAIN DATA IN FJU DATABASE."
        D. "FWD J7.0(ACT=1). RETAIN ID DATA IN FJU DATABASE."
        E. "FWD J7.0(ACT=0)."
        F. "FWD J7.0(ACT=2)."
        G. "FWD J7.0(ACT=4)."
        H. "FWD J10.5."
        I. "FWD J7.5."
        Z. "END TRANSLATION."


}
CONDITION {
    Node1: "ACTION, TRACK MANAGEMENT = 0."
    Node2: "ACTION, TRACK MANAGEMENT = 1."
    Node3: "ACTION, TRACK MANAGEMENT = 2."
    Node4: "ACTION, TRACK MANAGEMENT = 3."
    Node5: "ACTION, TRACK MANAGEMENT = 4."
    Node6: "CI = 0."
    Node7: "ACTION, TRACK MANAGEMENT = 5, 6, OR 7."
    Node8: "ACTION, TRACK MANAGEMENT = 6."
    Node9: "STATUS INDICATOR = 1."
}
IF (Node1) {
    IF (Node4) {
        EXECUTE(A, E, F, Z)


}
ELSE {
    IF (Node5) {
        EXECUTE(A, E, F, Z)


}
ELSE {
    IF (Node6) {
        EXECUTE(A, C, D, Z)


}
ELSE {
    IF (Node9) {
        EXECUTE(A, C, D, Z)


}
ELSE {
    EXECUTE(A, C, D, G, Z)
}
}
}
}
}
ELSE {
    IF (Node2) {
        IF (Node3) {
            IF (Node5) {
                IF (Node7) {
                    IF (Node8) {
                        IF (Node9) {
                            EXECUTE(A, C, G, H, Z)


                    }
                    ELSE {
                        EXECUTE(A, B, C, G, I, Z)


                }


        }
        ELSE {
            IF (Node9) {
                EXECUTE(A, C, G, Z)


        }
        ELSE {
            EXECUTE(A, C, G, Z)


    }
}
}
ELSE {
    EXECUTE(A, C, D, G, Z)
}
}
ELSE {
    EXECUTE(A, C, D, G, Z)
}
}
ELSE {
    IF (Node9) {
        EXECUTE(A, C, D, G, Z)


}
ELSE {
    EXECUTE(A, C, D, G, Z)
}
}
}
ELSE {
    IF (Node9) {
        EXECUTE(A, C, D, G, Z)


}
ELSE {
    EXECUTE(A, C, D, G, Z)
}
}
}
}