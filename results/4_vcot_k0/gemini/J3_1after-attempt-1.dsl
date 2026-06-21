Message TestNodeDiagram Translation Trees {
    ACTION {
        A. "Action A"
        B. "Action B"
        C. "Action C"
        D. "Action D"
        E. "Action E"
        F. "Action F"
        G. "Action G"
        H. "Action H"
        Z. "Action Z"


}
CONDITION {
    C1: "Node 1"
    C2: "Node 2"
    C3: "Node 3"
    C4: "Node 4"
    C5: "Node 5"
    C6: "Node 6"
    C7: "Node 6"
}
IF (C1) {
    EXECUTE(A, Z)
}
ELSE {
    IF (C2) {
        EXECUTE(B, G)


}
ELSE {
    IF (C3) {
        EXECUTE(C, G)


}
ELSE {
    IF (C4) {
        EXECUTE(D, G)


}
ELSE {
    IF (C5) {
        EXECUTE(E, F, G)


}
ELSE {
    IF (C6) {
        EXECUTE(F, H, Z)


}
ELSE {
    IF (C7) {
        EXECUTE(F, Z)


}
ELSE {
    EXECUTE(Z)
}
}
}
}
}
}
}
}
