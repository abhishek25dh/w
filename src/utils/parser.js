// src/utils/parser.js
export function parseChat(text) {
  const lines = text.split('\n');
  const messages = [];

  const regex = /^(\d{1,2}\/\d{1,2}\/\d{2,4}), (\d{1,2}:\d{2}) ?(AM|PM)? - (.*?): (.+)$/;

  for (let line of lines) {
    const match = line.match(regex);
    if (match) {
      const [_, date, time, ampm, sender, message] = match;
      messages.push({
        date,
        time: ampm ? `${time} ${ampm}` : time,
        sender,
        message,
      });
    }
  }

  return messages;
}
