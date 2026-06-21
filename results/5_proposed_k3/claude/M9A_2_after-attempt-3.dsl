Message M9A_Sheet3 Translation Trees {
    ACTION {
        A."DISCARD MESSAGE"
        L."FWD J7.1"
        M."FWD J7.5"
        N."RETAIN DATA IN FJU DATABASE"
        Z."END TRANSLATION"


}
CONDITION {
    C18:"PU/RU ADDRESS IS A UNIT TO WHOM DATA ARE BEING FORWARDED BY THIS FJU"
    C19:"STATUS INDICATOR = 1"
    C20:"TN = 0"
    C21:"ACTION = 9 AND MODE INDICATOR = 0, 2-4, 6, OR 7"
    C22:"IFF/SIF ACTION CODE = 0"
}
IF (C18) {
    EXECUTE(A, Z)
}
ELSE {
    IF (C19) {
        EXECUTE(A, Z)


}
ELSE {
    IF (C20) {
        EXECUTE(L, Z)


}
ELSE {
    IF (C21) {
        EXECUTE(A, M, N, Z)


}
ELSE {
    IF (C22) {
        EXECUTE(M, Z)


}
ELSE {
    EXECUTE(A, Z)
}
}
}
}
}
}
