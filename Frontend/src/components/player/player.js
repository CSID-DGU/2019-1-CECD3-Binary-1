import React, { useEffect } from 'react';

const Player = ({ index, url }) => {
  useEffect(() => {
    const script = document.createElement('script');
    script.type = 'text/javascript';
    script.innerHTML = `const canvas${index} = document.getElementById('video-canvas${index}');\
                        const player${index} = new JSMpeg.Player('${url}', {canvas: canvas${index}, autoplay: true});`
    document.body.appendChild(script);
    return () => {
      document.body.removeChild(script);
    }
  }, []);

  return (
    <canvas id={'video-canvas' + index} />
  );
}

export default Player;