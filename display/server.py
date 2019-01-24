import asyncio
from dataclasses import dataclass
from json import loads, dumps, JSONEncoder
from socket import socket, AF_INET, SOCK_STREAM as TCP
from typing import Tuple

import websockets as websockets


@dataclass
class Player:
    name: str = ""
    score: int = 0
    is_playing: bool = False


@dataclass
class Game:
    player1: Player
    player2: Player


class PlayerEncoder(JSONEncoder):
    def default(self, obj):
        if isinstance(obj, Player):
            return obj.__dict__
        # Let the base class default method raise the TypeError
        return JSONEncoder.default(self, obj)


def open_socket(address: Tuple[str, int]):
    sock = socket(AF_INET, TCP)
    sock.bind(address)
    sock.listen(1)

    connection, _ = sock.accept()

    return connection


async def get_data_from_app(reader, _):
    player1 = await recv_player(reader)
    player2 = await recv_player(reader)


async def recv_player(reader):
    byte_buffer = reader.read(4)

    data_size = int.from_bytes(byte_buffer, "big")

    json_data: bytes = reader.read(data_size)

    template = Player()
    template.__dict__ = loads(json_data.decode("utf-8"))

    return template


async def main(websocket, _):
    await websocket.send(dumps(GAME_STATE.__dict__, cls=PlayerEncoder))


if __name__ == "__main__":
    app_address = ("192.168.2.112", 40000)
    ws_address = "ws://localhost:8000"

    #from_app = open_socket(app_address)

    GAME_STATE = Game(Player("Ronnie"), Player("John"))

    start_server = websockets.serve(main, '127.0.0.1', 8000)

    asyncio.get_event_loop().run_until_complete(start_server)
    asyncio.get_event_loop().run_forever()
