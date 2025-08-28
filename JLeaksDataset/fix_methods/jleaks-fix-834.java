private boolean convert( FileObject file, boolean toUnix ) 
{
    if (toUnix) {
        toUnix(input, amount);
    } else {
        toDos(input, amount);
    }
}