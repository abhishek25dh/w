// src/App.jsx
import { useEffect, useState } from 'react';
import { parseChat } from './utils/parser';
import ChatMessage from './components/ChatMessage';

const PAGE_SIZE = 50;

function App() {
  const [messages, setMessages] = useState([]);
  const [page, setPage] = useState(0);

  useEffect(() => {
    fetch('/chat.txt')
      .then(res => res.text())
      .then(text => {
        const parsed = parseChat(text);
        setMessages(parsed);
      });
  }, []);

  const start = page * PAGE_SIZE;
  const end = start + PAGE_SIZE;
  const currentMessages = messages.slice(start, end);

  return (
    <div className="max-w-3xl mx-auto mt-6 px-4">
      <h1 className="text-2xl font-bold mb-4">📱 WhatsApp Chat Viewer</h1>
      <div className="border rounded shadow">
        {currentMessages.map((msg, i) => (
          <ChatMessage key={i} msg={msg} />
        ))}
      </div>

      <div className="flex justify-between mt-4">
        <button
          disabled={page === 0}
          onClick={() => setPage(page - 1)}
          className="px-4 py-2 bg-gray-200 hover:bg-gray-300 rounded disabled:opacity-50"
        >
          ⬅️ Back
        </button>
        <button
          disabled={end >= messages.length}
          onClick={() => setPage(page + 1)}
          className="px-4 py-2 bg-gray-200 hover:bg-gray-300 rounded disabled:opacity-50"
        >
          Next ➡️
        </button>
      </div>
    </div>
  );
}

export default App;
