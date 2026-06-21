Message M_14 Translation Trees {
    ACTION {
        A. "DISCARD MESSAGE."
        B. "FWD J13.2, J13.3, J13.4, OR J13.5 MESSAGE."
        C. "FWD J10.2 MESSAGE."
        Z. "END TRANSLATION."


}
CONDITION {
    1: "W/ES = 0, 1, OR 12."
    2: "W/ES = 2, 5, 6, 8, OR 10 AND WEAPON TYPE = 0-10."
    3: "W/ES = 3 AND WEAPON TYPE = 0-2, 4, 6-8, OR 10."
    4: "W/ES = 4 OR 7 AND WEAPON TYPE = 1-10."
    5: "W/ES =9 AND WEAPON TYPE = 0."
}
IF (1) {
    EXECUTE(B, Z)
}
ELSE {
    IF (2) {
        EXECUTE(C, Z)


}
ELSE {
    IF (3) {
        EXECUTE(C, Z)


}
ELSE {
    IF (4) {
        EXECUTE(C, Z)


}
ELSE {
    IF (5) {
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
