Message M9A Translation Trees {
    ACTION {
        A. "DISCARD MESSAGE."
        L. "FWD J7.1."
        Z. "END TRANSLATION."


}
CONDITION {
    18: "PU/RU ADDRESS IS A UNIT TO WHOM DATA ARE BEING FORWARDED BY THEIS FJU."
    19: "STATUS INDICATOR = 1."
    20: "TN = 0."
}
IF (18) {
    IF (19) {
        IF (20) {
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