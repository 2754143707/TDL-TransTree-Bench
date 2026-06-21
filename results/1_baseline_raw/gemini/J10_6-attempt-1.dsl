Message J10_6_to_Link_11_11B Translation Trees {
    ACTION {
        A. "DISCARD MESSAGE."
        B. "FWD M.9B."
        Z. "END TRANSLATION."


}
CONDITION {
    Node1: "PAIRING ACTION = 0-7 OR 15."
}
IF (Node1) {
    EXECUTE(A,B,Z)
}
ELSE {
    EXECUTE(Z)
}
}
