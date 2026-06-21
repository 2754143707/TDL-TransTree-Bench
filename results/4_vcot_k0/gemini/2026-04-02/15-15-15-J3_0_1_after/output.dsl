Message J3_0 Translation Trees {
    ACTION {
        A. "DISCARD MESSAGE."
        B. "FWD APPROPRIATE INITIAL SEQUENCE."
        C. "FWD M.5."
        Z. "END TRANSLATION."


}
CONDITION {
    Node1: "EXERCISE INDICATOR = 1."
    Node2: "PERIODIC REPORT INDICATOR = 2 WITH COURSE OR SPEED = NO STATEMENT."
    Node3: "POINT TYPE OR POINT AMPLIFICATION IS UNDEFINED OR HAS NO LINK 11/11B EQUIVALENT."
    Node4: "LINE/AREA CONTINUATION INDICATOR = 1."
    Node5: "POINT/LINE/AREA DESCRIPTOR, 1 = 2."
}
IF (Node1) {
    EXECUTE(B, Z)
}
ELSE {
    IF (Node2) {
        EXECUTE(C, Z)


}
ELSE {
    IF (Node3) {
        EXECUTE(C, Z)


}
ELSE {
    IF (Node4) {
        EXECUTE(C, Z)


}
ELSE {
    IF (Node5) {
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