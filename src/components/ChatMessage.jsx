// src/components/ChatMessage.jsx
export default function ChatMessage({ msg }) {
  return (
    <div className="p-2 border-b">
      <div className="text-sm text-gray-600">{msg.date} {msg.time}</div>
      <div>
        <strong>{msg.sender}:</strong> {msg.message}
      </div>
    </div>
  );
}
