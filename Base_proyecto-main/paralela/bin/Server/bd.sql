-- Creación de la base de datos
CREATE DATABASE riotGames;
USE riotGames;

-- Tabla de Jugadores
CREATE TABLE Jugador (
    nombre VARCHAR(50) PRIMARY KEY,
    clave INT
);

-- Tabla de Productos
CREATE TABLE Producto (
    nombreProducto VARCHAR(100) PRIMARY KEY,
    tipoProducto ENUM('skin', 'riotPoints') NOT NULL
);

-- Tabla de Comprobantes
CREATE TABLE Comprobante (
    idComprobante INT AUTO_INCREMENT PRIMARY KEY,
    nombreJugador VARCHAR(50),
    nombreProducto VARCHAR(100),
    tipoProducto ENUM('skin', 'riotPoints') NOT NULL,
    fecha DATE,
    precioTotal FLOAT,
    FOREIGN KEY (nombreJugador) REFERENCES Jugador(nombre),
    FOREIGN KEY (nombreProducto) REFERENCES Producto(nombreProducto)
);

-- Poblar datos Skins
INSERT INTO Producto (nombreProducto, tipoProducto)
VALUES
('Ahri Zorro de nueve colas', 'skin'),
('Ezreal pulso de fuego', 'skin'),
('Thresh Espiritu Cosechador', 'skin'),
('Lux Elementalista', 'skin'),
('Miss Fortune Artillera', 'skin'),
('Katarina Academia de Combate', 'skin'),
('Garen Reinos en Guerra', 'skin'),
('Sona DJ', 'skin'),
('Morgana Guardiana de las Estrellas', 'skin'),
('Lee Sin Puños Divinos', 'skin'),
('Annie en las Sombras', 'skin'),
('Olaf Vikingo', 'skin'),
('Zed Maestro de las Sombras', 'skin'),
('Ashe Arquera de Hielo', 'skin'),
('Teemo Explorador', 'skin'),
('Nasus Guardián de las Arenas', 'skin'),
('Diana Valquiria Oscura', 'skin'),
('Draven Gladiador', 'skin'),
('Akali Asesina de los Cielos', 'skin'),
('Talon Renegado', 'skin'),
('Yasuo Guardián de las Puertas', 'skin'),
('Yasuo Verdugo Nocturno', 'skin'),
('Zoe Cazadora de Estrellas', 'skin'),
('Zoe Viajera del Tiempo', 'skin'),
('Zed Destructor del Vacío', 'skin'),
('Zed Amo del Relámpago', 'skin'),
('Kayn Oscuro', 'skin'),
('Kayn Cazador de Almas', 'skin'),
('LeBlanc Máscara Espectral', 'skin'),
('LeBlanc Hechicera Arcana', 'skin'),
('Darius Rey de la Guerra', 'skin'),
('Darius Ejecutor', 'skin'),
('Orianna Corazón de Hierro', 'skin'),
('Orianna Inventora', 'skin'),
('Viktor Creador', 'skin'),
('Viktor Caos Mecánico', 'skin'),
('Syndra Reina Oscura', 'skin');

-- Poblar datos RP
INSERT INTO Producto (nombreProducto, tipoProducto) VALUES
('650 Riot Points', 'riotPoints'),
('1380 Riot Points', 'riotPoints'),
('2800 Riot Points', 'riotPoints'),
('5000 Riot Points', 'riotPoints'),
('7200 Riot Points', 'riotPoints'),
('15000 Riot Points', 'riotPoints');

-- Poblar Jugadores
INSERT INTO Jugador (nombre, clave) VALUES
('LunaStar', 12345),
('ShadowHawk', 23456),
('CrystalGem', 34567),
('IronWolf', 45678),
('MysticRiver', 56789);

-- Generar compras
INSERT INTO Comprobante (nombreJugador, nombreProducto, fecha, precioTotal) VALUES
('LunaStar', 'Ahri Zorro de nueve colas', CURDATE(), 520),
('ShadowHawk', 'Ezreal pulso de fuego', CURDATE(), 3250),
('CrystalGem', 'Thresh Espiritu Cosechador', CURDATE(), 1350),
('IronWolf', 'Lux Elementalista', CURDATE(), 3250),
('MysticRiver', 'Miss Fortune Artillera', CURDATE(), 975);
