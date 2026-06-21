Message M14 Translation Trees {
    ACTION {
        A. DISCARD MESSAGE
        B. FWD J13.2, J13.3, J13.4, OR J13.5 MESSAGE
        C. FWD J10.2 MESSAGE
        Z. END TRANSLATION


}
CONDITION {
    N1: W/ES = 0, 1, OR 12
    N2: W/ES = 2, 5, 6, 8, OR 10 AND WEAPON TYPE = 0-10
    N3: W/ES = 3 AND WEAPON TYPE = 0-2, 4, 6-8, OR 10
    N4: W/ES = 4 OR 7 AND WEAPON TYPE = 1-10
    N5: W/ES =9 AND WEAPON TYPE = 0
}
IF (N1) {
    EXECUTE(B, Z)
}
ELSE {
    IF (N2) {
        EXECUTE(C, Z)


}
ELSE {
    IF (N3) {
        EXECUTE(C, Z)


}
ELSE {
    IF (N4) {
        EXECUTE(C, Z)


}
ELSE {
    IF (N5) {
        EXECUTE(C, Z)


}
ELSE {
    EXECUTE(A, Z)
}
}
}
}
}
}
