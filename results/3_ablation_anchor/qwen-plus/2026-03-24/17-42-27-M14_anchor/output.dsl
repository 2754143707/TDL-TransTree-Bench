Message M14ToLink16 Translation Trees {
    ACTION {
        A. "DISCARD MESSAGE."
        B. "FWD J13.2, J13.3, J13.4, OR J13.5 MESSAGE."
        C. "FWD J10.2 MESSAGE."
        Z. "END TRANSLATION."


}
CONDITION {
    Node1: "W/ES = 0, 1, OR 12."
    Node2: "W/ES = 2, 5, 6, 8, OR 10 AND WEAPON TYPE = 0-10."
    Node3: "W/ES = 3 AND WEAPON TYPE = 0-2, 4, 6-8, OR 10."
    Node4: "W/ES = 4 OR 7 AND WEAPON TYPE = 1-10."
    Node5: "W/ES = 9 AND WEAPON TYPE = 0."
}
IF (Node1) {
    EXECUTE(A)
}
ELSE {
    IF (Node2) {
        EXECUTE(B)


}
ELSE {
    IF (Node3) {
        EXECUTE(C)


}
ELSE {
    IF (Node4) {
        EXECUTE(C)


}
ELSE {
    IF (Node5) {
        EXECUTE(C)


}
ELSE {
    EXECUTE(Z)
}
}
}
}
}
}