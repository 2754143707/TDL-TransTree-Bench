Message TABLE_C_5_1_M_9A_Sheet_3_of_8 Translation Trees {
    ACTION {
        A. "DISCARD MESSAGE."
        L. "FWD J7.1."
        M. "FWD J7.5."
        N. "RETAIN DATA IN FJU DATABASE."
        Z. "END TRANSLATION."


}
CONDITION {
    18: "PU/RU ADDRESS IS A UNIT TO WHOM DATA ARE BEING FORWARDED BY THEIS FJU."
    19: "STATUS INDICATOR = 1."
    20: "TN = 0."
    21: "ACTION = 9 AND MODE INDICATOR = 0, 2-4, 6, OR 7."
    22: "IFF/SIF ACTION CODE = 0."
}
IF (18) {
    IF (19) {
        EXECUTE(A, Z)


}
ELSE {
    EXECUTE(L, Z)
}
}
ELSE {
    IF (20) {
        EXECUTE(A, Z)


}
ELSE {
    IF (21) {
        IF (22) {
            EXECUTE(M, N, Z)


    }
    ELSE {
        EXECUTE(M, Z)


}
}
ELSE {
    EXECUTE(A, Z)
}
}
}
}