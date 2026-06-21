Message M9A Translation Trees {
    ACTION {
        A. "DISCARD MESSAGE."
        B. "Action B (Not provided in PDF)."
        C. "Action C (Not provided in PDF)."
        D. "Action D (Not provided in PDF)."
        E. "Action E (Not provided in PDF)."
        F. "Action F (Not provided in PDF)."
        G. "Action G (Not provided in PDF)."
        H. "Action H (Not provided in PDF)."
        I. "Action I (Not provided in PDF)."
        L. "FWD J7.1."
        M. "FWD J7.5."
        N. "RETAIN DATA IN FJU DATABASE."
        Z. "END TRANSLATION."


}
CONDITION {
    Node1: "Condition for Node 1 (Not provided in PDF)."
    Node2: "Condition for Node 2 (Not provided in PDF)."
    Node3: "Condition for Node 3 (Not provided in PDF)."
    Node4: "Condition for Node 4 (Not provided in PDF)."
    Node5: "Condition for Node 5 (Not provided in PDF)."
    Node6: "Condition for Node 6 (Not provided in PDF)."
    Node7: "Condition for Node 7 (Not provided in PDF)."
    Node8: "Condition for Node 8 (Not provided in PDF)."
    Node9: "Condition for Node 9 (Not provided in PDF)."
    Node18: "PU/RU ADDRESS IS A UNIT TO WHOM DATA ARE BEING FORWARDED BY THEIS FJU."
    Node19: "STATUS INDICATOR = 1."
    Node20: "TN = 0."
    Node21: "ACTION = 9 AND MODE INDICATOR = 0, 2-4, 6, OR 7."
    Node22: "IFF/SIF ACTION CODE = 0."
}
IF (Node1) {
    IF (Node4) {
        EXECUTE(E, F, Z)


}
ELSE {
    IF (Node5) {
        EXECUTE(A, Z)


}
ELSE {
    EXECUTE(A, C, D, Z)
}
}
}
ELSE {
    IF (Node2) {
        IF (Node6) {
            IF (Node9) {
                EXECUTE(C, D, G, Z)


        }
        ELSE {
            EXECUTE(C, D, G, Z)


    }
}
ELSE {
    IF (Node9) {
        EXECUTE(C, D, G, Z)


}
ELSE {
    EXECUTE(C, D, G, Z)
}
}
}
ELSE {
    IF (Node3) {
        IF (Node5) {
            EXECUTE(A, Z)


    }
    ELSE {
        EXECUTE(A, C, D, Z)


}
}
ELSE {
    IF (Node7) {
        IF (Node8) {
            IF (Node9) {
                EXECUTE(G, I, Z)


        }
        ELSE {
            EXECUTE(G, I, Z)


    }
}
ELSE {
    EXECUTE(G, H, Z)
}
}
ELSE {
    EXECUTE(A, B, C, Z)
}
}
}
}
}