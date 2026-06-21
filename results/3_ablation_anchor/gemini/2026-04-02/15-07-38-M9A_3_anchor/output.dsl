Message M_9A_1 Translation Trees {
    ACTION {
        A. "DISCARD MESSAGE."
        L. "FWD J7.1."
        Z. "END TRANSLATION."


}
CONDITION {
    C18: "PU/RU ADDRESS IS A UNIT TO WHOM DATA ARE BEING FORWARDED BY THEIS FJU."
    C19: "STATUS INDICATOR = 1."
    C20: "TN = 0."
}
IF (C18) {
    IF (C19) {
        IF (C20) {
            EXECUTE(L, Z)


    }
    ELSE {
        EXECUTE(A, Z)


}
}
ELSE {
    EXECUTE(A, Z)
}
}
ELSE {
    EXECUTE(A, Z)
}
}