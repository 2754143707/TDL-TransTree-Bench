Message M_6D Translation Trees {
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
    10: "CONTROL = 5."
    11: "FREQUENCY/FREQUENCY RANGE = 0."
    12: "EMITTER FUNCTION AND EMITTER NUMBER = 0."
    13: "CONTROL = 7, 12, OR 13 AND REFERENCED/ CANCELLED CONTROL VALUE IS OTHER THAN 5 THROUGH 13."
    14: "CONTROL = 14 OR 15."
}
IF (10) {
    IF (11) {
        IF (12) {
            EXECUTE (G, Z)


    }
    ELSE {
        EXECUTE (H, Z)


}
}
ELSE {
    IF (12) {
        EXECUTE (J, Z)


}
ELSE {
    EXECUTE (I, Z)
}
}
}
ELSE {
    IF (13) {
        EXECUTE (C, Z)


}
ELSE {
    IF (14) {
        EXECUTE (G, Z)


}
ELSE {
    EXECUTE (A, Z)
}
}
}
}