Message M9A Translation Trees {
    ACTION {
        A. "DISCARD MESSAGE."
        L. "FWD J7.1."
        M. "FWD J7.5."
        N. "RETAIN DATA IN FJU DATABASE."
        Z. "END TRANSLATION."


}
CONDITION {
    Node_18: "PU/RU ADDRESS IS A UNIT TO WHOM DATA ARE BEING FORWARDED BY THEIS FJU."
    Node_19: "STATUS INDICATOR = 1."
    Node_20: "TN = 0."
    Node_21: "ACTION = 9 AND MODE INDICATOR = 0, 2-4, 6, OR 7."
    Node_22: "IFF/SIF ACTION CODE = 0."
}
IF (Node_18) {
    EXECUTE(A, Z)
}
ELSE {
    IF (Node_19) {
        EXECUTE(A, Z)


}
ELSE {
    IF (Node_20) {
        EXECUTE(A, Z)


}
ELSE {
    IF (Node_21) {
        IF (Node_22) {
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
}
