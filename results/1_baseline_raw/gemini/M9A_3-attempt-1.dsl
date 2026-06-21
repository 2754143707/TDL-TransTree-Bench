Message M9A Translation Trees {
    ACTION {
        A. "DISCARD MESSAGE."
        L. "FWD J7.1."
        M. "FWD J7.5."
        N. "RETAIN DATA IN FJU DATABASE."
        Z. "END TRANSLATION."


}
CONDITION {
    Node18: "PU/RU ADDRESS IS A UNIT TO WHOM DATA ARE BEING FORWARDED BY THEIS FJU."
    Node19: "STATUS INDICATOR = 1."
    Node20: "TN = 0."
    Node21: "ACTION = 9 AND MODE INDICATOR = 0, 2-4, 6, OR 7."
    Node22: "IFF/SIF ACTION CODE = 0."
}
IF (Node18) {
    IF (Node19) {
        IF (Node20) {
            EXECUTE(L,Z)


    }
    ELSE {
        IF (Node21) {
            IF (Node22) {
                EXECUTE(M,N,Z)


        }
        ELSE {
            EXECUTE(M,Z)


    }
}
ELSE {
    EXECUTE(A,Z)
}
}
}
ELSE {
    EXECUTE(A,Z)
}
}
ELSE {
    EXECUTE(A,Z)
}
}
