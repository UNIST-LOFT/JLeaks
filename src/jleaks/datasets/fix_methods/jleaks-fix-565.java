public void close(){
    if (!closed) {
        closed = true;
        try {
            itty.close();
        } finally {
            baton.release("iterator");
        }
    }
}