import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip, Legend } from 'recharts';
import QuestionModal from '../QuestionModal';

const COLORS = ['#0088FE', '#00C49F', '#FFBB28', '#FF8042', '#8884d8', '#82ca9d', '#ffc658'];

const MultithreadingPitfalls = () => {
  const [data, setData] = useState([]);
  const [selectedPitfall, setSelectedPitfall] = useState(null);
  const [modalOpen, setModalOpen] = useState(false);
  const [selectedQuestionId, setSelectedQuestionId] = useState(null);

  useEffect(() => {
    axios.get('/api/stats/multithreading-pitfalls', { params: { topN: 7 } })
      .then(res => setData(res.data))
      .catch(console.error);
  }, []);

  const handleSliceClick = (data, index) => {
    setSelectedPitfall(data);
  };

  const openQuestion = (id) => {
    setSelectedQuestionId(id);
    setModalOpen(true);
  };

  return (
    <div className="flex flex-col md:flex-row gap-8">
      {/* Chart Section - Takes remaining space (approx 70%) */}
      <div className="flex-1 min-h-[300px]">
        <ResponsiveContainer width="100%" height={350}>
          <PieChart>
            <Pie
              data={data}
              cx="50%"
              cy="50%"
              labelLine={false}
              outerRadius={120}
              fill="#8884d8"
              dataKey="count"
              nameKey="pitfall"
              onClick={handleSliceClick}
              cursor="pointer"
            >
              {data.map((entry, index) => (
                <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
              ))}
            </Pie>
            <Tooltip contentStyle={{ backgroundColor: 'hsl(var(--card))' }} />
            <Legend layout="vertical" align="right" verticalAlign="middle" />
          </PieChart>
        </ResponsiveContainer>
        <p className="text-center text-sm text-muted-foreground mt-2">
          Click a slice to view related questions
        </p>
      </div>

      {/* List Section - Fixed at 30% width on Desktop */}
      <div className="w-full md:w-[30%] shrink-0 border rounded-md p-4 h-[350px] overflow-y-auto bg-muted/20">
        <h4 className="font-semibold mb-3 sticky top-0 bg-background/0 backdrop-blur-sm">
          {selectedPitfall ? `${selectedPitfall.pitfall} Questions` : 'Select a pitfall to view questions'}
        </h4>
        
        {selectedPitfall ? (
          <div className="space-y-2">
            {selectedPitfall.question_ids.map(id => (
              <button
                key={id}
                onClick={() => openQuestion(id)}
                className="group w-full text-left px-3 py-2 text-sm rounded hover:bg-accent hover:text-accent-foreground transition-colors flex items-center gap-2"
              >
                {/* Added group-hover effect specific to the ID */}
                <span className="text-primary font-mono group-hover:underline group-hover:font-bold"># {id}</span>
              </button>
            ))}
          </div>
        ) : (
          <div className="flex h-full items-center justify-center text-muted-foreground">
             Select a chart segment
          </div>
        )}
      </div>

      <QuestionModal 
        isOpen={modalOpen} 
        onClose={() => setModalOpen(false)} 
        questionId={selectedQuestionId} 
      />
    </div>
  );
};

export default MultithreadingPitfalls;
