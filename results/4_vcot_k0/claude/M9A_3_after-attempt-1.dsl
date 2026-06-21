Message M9A_Sheet3 Translation Trees {
    ACTION {
        A.DISCARD MESSAGE
        L.FWD J7.1
        M.FWD J7.5
        N.RETAIN DATA IN FJU DATABASE
        Z.END TRANSLATION


}
CONDITION {
    N18:PU/RU ADDRESS IS A UNIT TO WHOM DATA ARE BEING FORWARDED BY THIS FJU
    N19:STATUS INDICATOR = 1
    N20:TN = 0
    N21:ACTION = 9 AND MODE INDICATOR = 0, 2-4, 6, OR 7
    N22:IFF/SIF ACTION CODE = 0
}
IF (N18) {
    IF (N19) {
        IF (N20) {
            EXECUTE(L, Z)


    }
    ELSE {
        IF (N21) {
            IF (N22) {
                EXECUTE(A, M, N, Z)


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
ELSE {
    EXECUTE(A, Z)
}
}
ELSE {
    EXECUTE(A, Z)
}
}
