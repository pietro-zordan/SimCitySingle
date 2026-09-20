package model;

/*
 * Memorizza una costruzione distrutta da ricostruire
 * nella stessa posizione e con lo stesso stato.
 */
public record ReconstructionEntry(
        Construction construction,
        int row,
        int column)
{
}
