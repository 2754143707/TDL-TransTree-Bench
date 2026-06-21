Message M_9A_1 Translation Trees {
    ACTION {
        A. "DISCARD MESSAGE."
        L. "FWD J7.1."
        Z. "END TRANSLATION."


}
CONDITION {
    Node18: "PU/RU ADDRESS IS A UNIT TO WHOM DATA ARE BEING FORWARDED BY THEIS FJU."
    Node19: "STATUS INDICATOR = 1."
    Node20: "TN = 0."
}
IF (Node18) {
    IF (Node19) {
        IF (Node20) {
            EXECUTE (L, Z)


    }
    ELSE {
        EXECUTE (A, Z)


}
}
ELSE {
    EXECUTE (A, Z)
}
}
ELSE {
    EXECUTE (A, Z)
}
}