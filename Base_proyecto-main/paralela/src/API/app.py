import json, os
from flask import Flask, request, g, jsonify

app = Flask(__name__)
script_path = os.path.realpath(__file__).rstrip("app.py")

# Función para abrir y cargar el archivo de skins
def open_skins():
    filename = os.path.join(script_path, "skins.json")
    with open(filename, "r", encoding='utf-8') as file:
        g.skins = json.load(file)

# Función para asegurarse de que las skins están cargadas
def read_skins():
    if 'skins' not in g:
        open_skins()

# Función para abrir y cargar el archivo de Riot Points
def open_riot_points():
    filename = os.path.join(script_path, "rp.json")
    with open(filename, "r", encoding='utf-8') as file:
        g.riot_points = json.load(file)

# Función para asegurarse de que los Riot Points están cargados
def read_riot_points():
    if 'riot_points' not in g:
        open_riot_points()

# Endpoint para obtener skins por personaje o por nombre de skin
@app.route('/skins', methods=['GET'])
def get_skins_by_character_or_price():
    read_skins()
    character_name = request.args.get('character', type=str)
    if character_name:
        matching_skins = [skin for skin_id, skin in g.skins.items() if skin.get("personaje", "").lower() == character_name.lower()]
        if not matching_skins:
            return {"error": "No se encontraron skins para el personaje especificado"}, 404
        return jsonify(matching_skins), 200

    skin_name = request.args.get('name', type=str)
    if skin_name:
        for skin_id, skin in g.skins.items():
            if skin["nombreSkin"].lower() == skin_name.lower():
                precio = skin["descripcion"]["precio"]
                descuento = skin["descripcion"]["valorDescuento"]
                precio_descuento = precio - (precio * (descuento / 100))
                return jsonify({"nombreSkin": skin_name, "precioNormal": precio, "precioConDescuento": precio_descuento}), 200
        return {"error": "La skin no existe"}, 404

    return {"error": "Debe proporcionar un parámetro de búsqueda"}, 400

# Nuevo endpoint para obtener precios de Riot Points
@app.route('/rp', methods=['GET'])
def riot_points():
    read_riot_points()
    return jsonify(g.riot_points), 200

if __name__ == '__main__':
    app.run(debug=True)


