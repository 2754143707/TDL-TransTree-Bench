Message M6D Translation Trees {
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
    N10: "CONTROL = 5."
    N11: "FREQUENCY/FREQUENCY RANGE = 0."
    N12: "EMITTER FUNCTION AND EMITTER NUMBER = 0."
    N13: "CONTROL = 7, 12, OR 13 AND REFERENCED/ CANCELLED CONTROL VALUE IS OTHER THAN 5 THROUGH 13."
    N14: "CONTROL = 14 OR 15."
}
IF (N10) {
    IF (N11) {
        IF (N12) {
            EXECUTE(G, Z)


    }
    ELSE {
        EXECUTE(H, Z)


}
}
ELSE {
    IF (N12) {
        EXECUTE(J, Z)


}
ELSE {
    EXECUTE(I, Z)
}
}
}
ELSE {
    IF (N13) {
        EXECUTE(C, Z)


}
ELSE {
    IF (N14) {
        EXECUTE(G, Z)


}
ELSE {
    EXECUTE(A, Z)
}
}
}
}