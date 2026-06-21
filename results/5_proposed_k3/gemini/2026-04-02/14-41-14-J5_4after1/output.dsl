Message MSG Translation Trees {
    ACTION {
        A. "Action A"
        Z. "Action Z"


}
CONDITION {
    C1: "Condition 1"
}
IF (C1) {
    EXECUTE (A, Z)
}
ELSE {
    EXECUTE (A)
}
}