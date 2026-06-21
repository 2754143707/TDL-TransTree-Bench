Message M6D_Sheet_2 Translation Trees {
    ACTION {
        A. "DISCARD MESSAGE."
        C. "FWD J14.2I/J14.2E0."
        G. "FWD J14.2I/J14.2E0/J14.2C1."
        H. "FWD J14.2I/J14.2E0/J14.2C1/J14.2C3."
        I. "FWD J14.2I/J14.2E0/J14.2C1/J14.2C3/ J14.2C4."
        J. "FWD J14.2I/J142E0/J14.2C1/J14.2C4."
        Z. "END TRANSLATION."


}
CONDITION {
    Node10: "CONTROL = 5."
    Node11: "FREQUENCY/FREQUENCY RANGE = 0."
    Node12: "EMITTER FUNCTION AND EMITTER NUMBER = 0."
    Node13: "CONTROL = 7, 12, OR 13 AND REFERENCED/ CANCELLED CONTROL VALUE IS OTHER THAN 5 THROUGH 13."
    Node14: "CONTROL = 14 OR 15."
}
IF (Node10) {
    IF (Node11) {
        IF (Node12) {
            EXECUTE(G, Z)


    }
    ELSE {
        EXECUTE(H, Z)


}
}
ELSE {
    IF (Node12) {
        EXECUTE(J, Z)


}
ELSE {
    EXECUTE(I, Z)
}
}
}
ELSE {
    IF (Node13) {
        EXECUTE(C, Z)


}
ELSE {
    IF (Node14) {
        EXECUTE(G, Z)


}
ELSE {
    EXECUTE(A, Z)
}
}
}
}