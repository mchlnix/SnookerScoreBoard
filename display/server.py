import asyncio
from dataclasses import dataclass
from json import loads, dumps, JSONEncoder, JSONDecoder
from json.decoder import WHITESPACE

import websockets as websockets


@dataclass
class Player:
    name: str = ""
    score: int = 0
    breaks: int = 0
    is_playing: bool = False


@dataclass
class Game:
    breaks: int = 0
    player1: Player = Player()
    player2: Player = Player()


class GameEncoder(JSONEncoder):
    def default(self, obj):
        if isinstance(obj, (Game, Player)):
            return obj.__dict__

        return JSONEncoder.default(self, obj)


class GameDecoder(JSONDecoder):
    def decode(self, s, _w=WHITESPACE.match):
        inner_dict = super().decode(s, _w)

        game = Game()
        game.player1.__dict__ = inner_dict["player1"]
        game.player2.__dict__ = inner_dict["player2"]

        return game


async def main(websocket, _):
    global STATE_DIRTY

    while not websocket.closed:
        if STATE_DIRTY:
            await websocket.send(dumps(GAME_STATE, cls=GameEncoder))
            STATE_DIRTY = False
        await asyncio.sleep(1)


async def handle_echo(reader, _):
    global GAME_STATE, STATE_DIRTY

    while reader:
        byte_buffer = await reader.read(4)

        if len(byte_buffer) != 4:
            break

        data_size = int.from_bytes(byte_buffer, "big")

        json_data: bytes = await reader.read(data_size)

        GAME_STATE = loads(json_data.decode())

        STATE_DIRTY = True


async def start_tcp_server():
    server = await asyncio.start_server(
        handle_echo, '192.168.2.112', 8888)

    addr = server.sockets[0].getsockname()
    print(f'Serving on {addr}')

    async with server:
        await server.serve_forever()


async def start_ws_server():
    print("ws server")
    await websockets.serve(main, '127.0.0.1', 8000)


if __name__ == "__main__":
    app_address = ("192.168.2.112", 40000)
    ws_address = "ws://localhost:8000"

    # from_app = open_socket(app_address)

    GAME_STATE = Game(Player("Ronnie O'Sullivan", 45), Player("John Higgins", 23))
    STATE_DIRTY = True

    loop = asyncio.get_event_loop()

    loop.create_task(start_tcp_server())
    loop.create_task(start_ws_server())
    loop.run_forever()
