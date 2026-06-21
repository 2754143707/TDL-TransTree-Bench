Message M9A_TranslationTree_Sheet3_Part2 Translation Trees {
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
IF (Node10) {
    EXECUTE(J, N, Z)
}
ELSE {
    IF (Node11) {
        EXECUTE(K, N, Z)


}
ELSE {
    IF (Node12) {
        EXECUTE(N, Z)


}
ELSE {
    IF (Node13) {
        IF (Node14) {
            IF (Node15) {
                EXECUTE(P, Z)


        }
        ELSE {
            EXECUTE(Q, Z)


    }
}
ELSE {
    EXECUTE(N, O, Z)
}
}
ELSE {
    IF (Node16) {
        IF (Node17) {
            EXECUTE(S, Z)


    }
    ELSE {
        EXECUTE(T, U, Z)


}
}
ELSE {
    EXECUTE(R, Z)
}
}
}
}
}
}