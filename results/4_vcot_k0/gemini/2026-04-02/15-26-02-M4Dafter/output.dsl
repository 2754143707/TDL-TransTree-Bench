Message UNKNOWN Translation Trees {
    ACTION {
        A. "Action A"
        B. "Action B"
        Z. "Action Z"


}
CONDITION {
    1: "Condition 1"
    2: "Condition 2"
    3: "Condition 3"
    4: "Condition 4"
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