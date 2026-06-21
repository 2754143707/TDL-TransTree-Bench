Message M9A_10 Translation Trees {
    ACTION {
        J . "UNKNOWN"
        K . "UNKNOWN"
        N . "RETAIN DATA IN FJU DATABASE."
        Z . "END TRANSLATION."


}
CONDITION {
    10 : "UNKNOWN"
}
IF (10) {
    EXECUTE(J, N, Z)
}
ELSE {
    EXECUTE(K, N, Z)
}
}