Message M Translation Trees {
    ACTION {
        A."A"
        B."B"
        Z."Z"


}
CONDITION {
    C1:"Test Node 1"
    C2:"Test Node 2"
    C3:"Test Node 3"
    C4:"Test Node 4"
}
IF (C1) {
    IF (C2) {
        IF (C3) {
            IF (C4) {
                EXECUTE(A, Z)


        }
        ELSE {
            EXECUTE(A, B, Z)


    }
}
ELSE {
    IF (C4) {
        EXECUTE(A, Z)


}
ELSE {
    EXECUTE(A, B, Z)
}
}
}
ELSE {
    IF (C3) {
        IF (C4) {
            EXECUTE(A, Z)


    }
    ELSE {
        EXECUTE(A, B, Z)


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