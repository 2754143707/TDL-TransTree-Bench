Message TEST_NODE_DIAGRAM Translation Trees {
    ACTION {
        A. "A"
        B. "B"
        Z. "Z"


}
CONDITION {
    1: "1"
    2: "2"
    3: "3"
    4: "4"
}
IF (1) {
    IF (2) {
        IF (3) {
            IF (4) {
                EXECUTE (A, Z)


        }
        ELSE {
            EXECUTE (B, Z)


    }
}
ELSE {
    EXECUTE (B, Z)
}
}
ELSE {
    IF (3) {
        IF (4) {
            EXECUTE (A, Z)


    }
    ELSE {
        EXECUTE (B, Z)


}
}
ELSE {
    EXECUTE (B, Z)
}
}
}
ELSE {
    EXECUTE (A, Z)
}
}
