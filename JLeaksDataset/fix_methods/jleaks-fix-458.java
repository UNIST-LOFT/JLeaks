public static void writeMainScoreboard(File path, GlowScoreboard scoreboard) throws IOException 
{
    CompoundTag root = new CompoundTag();
    CompoundTag data = new CompoundTag();
    root.putCompound("data", data);
    try (NBTOutputStream nbt = new NBTOutputStream(getDataOutputStream(path), true)) {
        writeObjectives(data, scoreboard);
        writeScores(data, scoreboard);
        writeTeams(data, scoreboard);
        writeDisplaySlots(data, scoreboard);
        nbt.writeTag(root);
        nbt.close();
    }
}