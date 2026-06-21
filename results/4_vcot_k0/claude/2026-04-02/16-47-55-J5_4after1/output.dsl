Message M Translation Trees {
    ACTION {
        A. A
        B. B
        C. C
        D. D
        E. E


}
CONDITION {
    C1: Test Node 1
    C2: Test Node 2
    C3: Test Node 3
}
IF (C1) {
    IF (C3) {
        EXECUTE(B, E)


}
ELSE {
    EXECUTE(A, E)
}
}
ELSE {
    IF (C2) {
        IF (C3) {
            EXECUTE(B, E)


    }
    ELSE {
        EXECUTE(A, D, E)


}
}
ELSE {
    IF (C3) {
        EXECUTE(C, E)


}
ELSE {
    EXECUTE(C, E)
}
}
}
}