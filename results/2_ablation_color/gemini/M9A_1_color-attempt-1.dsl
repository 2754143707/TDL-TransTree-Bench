Message M9A_Link16 Translation Trees {
    ACTION {
        A. "DISCARD MESSAGE."
        B. "Action B (Description Missing from PDF)."
        C. "Action C (Description Missing from PDF)."
        D. "Action D (Description Missing from PDF)."
        E. "Action E (Description Missing from PDF)."
        F. "Action F (Description Missing from PDF)."
        G. "Action G (Description Missing from PDF)."
        H. "Action H (Description Missing from PDF)."
        I. "Action I (Description Missing from PDF)."
        L. "FWD J7.1."
        M. "FWD J7.5."
        N. "RETAIN DATA IN FJU DATABASE."
        Z. "END TRANSLATION."


}
CONDITION {
    Node1: "Condition for Node 1 (Text Missing from PDF)."
    Node2: "Condition for Node 2 (Text Missing from PDF)."
    Node3: "Condition for Node 3 (Text Missing from PDF)."
    Node4: "Condition for Node 4 (Text Missing from PDF)."
    Node5: "Condition for Node 5 (Text Missing from PDF)."
    Node6: "Condition for Node 6 (Text Missing from PDF)."
    Node7: "Condition for Node 7 (Text Missing from PDF)."
    Node8: "Condition for Node 8 (Text Missing from PDF)."
    Node9: "Condition for Node 9 (Text Missing from PDF)."
}
IF (Node1) {
    IF (Node4) {
        EXECUTE(E, F, Z)


}
ELSE {
    IF (Node5) {
        EXECUTE(A, C, D, Z)


}
ELSE {
    IF (Node9) {
        EXECUTE(F, Z)


}
ELSE {
    EXECUTE(L, M, N, Z)
}
}
}
}
ELSE {
    IF (Node2) {
        IF (Node6) {
            IF (Node9) {
                EXECUTE(F, Z)


        }
        ELSE {
            EXECUTE(L, M, N, Z)


    }
}
ELSE {
    EXECUTE(A, C, D, Z)
}
}
ELSE {
    IF (Node3) {
        IF (Node5) {
            EXECUTE(A, C, D, Z)


    }
    ELSE {
        IF (Node9) {
            EXECUTE(F, Z)


    }
    ELSE {
        EXECUTE(L, M, N, Z)


}
}
}
ELSE {
    IF (Node7) {
        IF (Node8) {
            IF (Node9) {
                EXECUTE(G, Z)


        }
        ELSE {
            EXECUTE(G, Z)


    }
}
ELSE {
    EXECUTE(G, H, I, Z)
}
}
ELSE {
    EXECUTE(A, B, Z)
}
}
}
}
}
