Message M4D_M84D_M4B_Translation_Tree Translation Trees {
    ACTION {
        A. "DISCARD MESSAGE."
        B. "FWD J5.4 MESSAGE."
        Z. "END TRANSLATION."


}
CONDITION {
    Node1: "RECEIVED M.4D IS FOLLOWED BY AN M.84D MESSAGE AND RECEIVED M.4D/M.84D MESSAGE SEQUENCE IS LEGAL."
    Node2: "RECEIVED M.4D/M.84D IS FOLLOWED BY AN M.4B MESSAGE."
    Node3: "BEARING REPORT TYPE = 1."
    Node4: "M.84D(SW=0) BEARING INDICATOR = 1 OR M.84D(SW=1) BEARING INDICATOR = 1, OR NO M.84D(SW=1) RECEIVED."
}
IF (Node1) {
    IF (Node2) {
        IF (Node3) {
            IF (Node4) {
                EXECUTE(B, Z)


        }
        ELSE {
            EXECUTE(A)


    }
}
ELSE {
    EXECUTE(A)
}
}
ELSE {
    EXECUTE(A)
}
}
ELSE {
    EXECUTE(A)
}
}
