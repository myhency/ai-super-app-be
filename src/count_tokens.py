import sys
import tiktoken

def count_tokens(model, text):
    try:
        encoding = tiktoken.encoding_for_model(model)
    except Exception:
        encoding = tiktoken.get_encoding("cl100k_base")
    tokens = encoding.encode(text)
    return len(tokens)

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python count_tokens.py <model>")
        sys.exit(1)
    model = sys.argv[1]
    text = sys.stdin.read()
    token_count = count_tokens(model, text)
    print(token_count)