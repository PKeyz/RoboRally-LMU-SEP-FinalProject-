package bb.roborally.protocol.map;

import bb.roborally.protocol.Position;
import bb.roborally.protocol.map.tiles.Tile;

import java.util.ArrayList;

/**
 * @author Bence Ament
 */

public class Cell {
    private ArrayList<Tile> tiles = new ArrayList<>();

    private Position position;
    public Cell() {

    }

    public Cell(ArrayList<Tile> tiles) {
        this.tiles = tiles;
    }

    public ArrayList<Tile> getTiles() {
        return tiles;
    }

    public void setTiles(ArrayList<Tile> tiles) {
        this.tiles = tiles;
    }

    public void addTile(Tile tile) {
        tiles.add(tile);
    }

    public Tile getTile(int index) {
        return tiles.get(index);
    }

    public Tile getTile(String type) {
        for (Tile tile: tiles) {
            if (tile.getType().equals(type)) {
                return tile;
            }
        }
        return null;
    }

    public int getTileCount() {
        return tiles.size();
    }

    public boolean hasTile(String type) {
        for (Tile tile: tiles) {
            if (tile != null && tile.getType().equals(type)) {
                return true;
            }
        }
        return false;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[");
        int i = 0;
        for (Tile tile: getTiles()) {
            stringBuilder.append(tile.getType());
            if (i != getTileCount() - 1) {
                stringBuilder.append(", ");
            }
            i += 1;
        }
        stringBuilder.append("]");
        return stringBuilder.toString();
    }
}
