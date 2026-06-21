Message M9A Translation Trees {
    ACTION {
        A. "DISCARD MESSAGE."
        L. "FWD J7.1."
        M. "FWD J7.5."
        N. "RETAIN DATA IN FJU DATABASE."
        Z. "END TRANSLATION."
        J. "Action J from diagram."
        K. "Action K from diagram."
        P. "Action P from diagram."
        Q. "Action Q from diagram."
        R. "Action R from diagram."
        S. "Action S from diagram."
        U. "Action U from diagram."


}
CONDITION {
    Node10: "Condition for Node 10."
    Node11: "Condition for Node 11."
    Node12: "Condition for Node 12."
    Node13: "Condition for Node 13."
    Node14: "Condition for Node 14."
    Node15: "Condition for Node 15."
    Node16: "Condition for Node 16."
    Node17: "Condition for Node 17."
}
IF (Node10) {
    EXECUTE(J, Z)
}
ELSE {
    IF (Node11) {
        EXECUTE(K, Z)


}
ELSE {
    IF (Node12) {
        EXECUTE(N, Z)


}
ELSE {
    IF (Node13) {
        IF (Node14) {
            EXECUTE(P, Z)


    }
    ELSE {
        IF (Node15) {
            EXECUTE(Q, Z)


    }
    ELSE {
        IF (Node16) {
            EXECUTE(R, Z)


    }
    ELSE {
        IF (Node17) {
            EXECUTE(S, Z)


    }
    ELSE {
        EXECUTE(U, Z)


}
}
}
}
}
ELSE {
    IF (Node16) {
        EXECUTE(R, Z)


}
ELSE {
    IF (Node17) {
        EXECUTE(S, Z)


}
ELSE {
    EXECUTE(U, Z)
}
}
}
}
}
}
}
