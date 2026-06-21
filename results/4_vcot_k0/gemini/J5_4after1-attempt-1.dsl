Message Diagram Translation Trees {
    ACTION {
        A."Action A"
        B."Action B"
        C."Action C"
        D."Action D"
        E."Action E"


}
CONDITION {
    1:"Condition 1"
    2:"Condition 2"
    3:"Condition 3"
}
IF (1) {
    IF (3) {
        EXECUTE (B, E)


}
ELSE {
    EXECUTE (A, E)
}
}
ELSE {
    IF (2) {
        IF (3) {
            EXECUTE (B, E)


    }
    ELSE {
        EXECUTE (A, E)


}
}
ELSE {
    IF (3) {
        EXECUTE (D, E)


}
ELSE {
    EXECUTE (C, E)
}
}
}
}
