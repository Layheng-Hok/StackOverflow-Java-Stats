import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Cell } from 'recharts';
import { X } from 'lucide-react';
import ChartSkeleton from '../ChartSkeleton';

const TopicCooccurrences = () => {
  const [data, setData] = useState([]);
  const [excludeInput, setExcludeInput] = useState('');
  const [excludedTags, setExcludedTags] = useState([]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchData();
  }, [excludedTags]);

  const fetchData = async () => {
    setLoading(true);
    try {
      const response = await axios.get('/api/stats/topic-cooccurrences', {
        params: {
          topN: 10,
          minFrequency: 10,
          excludeTags: excludedTags.join(',')
        }
      });
      const chartData = response.data.map(item => ({
        pair: item.pair.join(' + '),
        frequency: item.frequency
      }));
      setData(chartData);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleAddExclude = async (e) => {
    e.preventDefault();
    if (!excludeInput) return;

    try {
      const checkRes = await axios.get('/api/tags/check', { params: { tagName: excludeInput } });
      if (checkRes.data) {
        if (!excludedTags.includes(excludeInput)) {
          setExcludedTags([...excludedTags, excludeInput]);
        }
        setExcludeInput('');
        setError('');
      } else {
        setError(`Tag '${excludeInput}' not found.`);
      }
    } catch (err) {
      setError("Error validating tag.");
    }
  };

  const removeTag = (tag) => {
    setExcludedTags(excludedTags.filter(t => t !== tag));
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-4">
        <form onSubmit={handleAddExclude} className="flex gap-2 items-center">
          <input 
            type="text" 
            placeholder="Exclude a tag..." 
            className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 max-w-sm"
            value={excludeInput}
            onChange={(e) => setExcludeInput(e.target.value)}
          />
          <button type="submit" className="h-10 px-4 py-2 bg-primary text-primary-foreground hover:bg-primary/90 rounded-md text-sm font-medium">
            Exclude
          </button>
        </form>
        {error && <span className="text-destructive text-sm">{error}</span>}
        
        <div className="flex flex-wrap gap-2">
          {excludedTags.map(tag => (
            <span key={tag} className="inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-semibold transition-colors focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2 border-transparent bg-secondary text-secondary-foreground hover:bg-secondary/80">
              {tag}
              <button onClick={() => removeTag(tag)} className="ml-1 hover:text-destructive">
                <X size={14} />
              </button>
            </span>
          ))}
        </div>
      </div>

      <div className="h-[400px] w-full">
        {loading ? (
           <ChartSkeleton height="h-full" />
        ) : (
           <ResponsiveContainer width="100%" height="100%">
             <BarChart
               layout="vertical"
               data={data}
               margin={{ top: 5, right: 30, left: 100, bottom: 20 }}
             >
               <CartesianGrid strokeDasharray="3 3" horizontal={false} vertical={true} className="stroke-muted" />
               
               <XAxis 
                 type="number" 
                 className="text-xs font-medium"
                 tick={{fill: 'hsl(var(--foreground))'}}
               />
               
               <YAxis 
                 type="category" 
                 dataKey="pair" 
                 width={150} 
                 className="text-xs font-medium"
                 tick={{fill: 'hsl(var(--foreground))'}}
               />
               <Tooltip 
                  cursor={{fill: 'hsl(var(--muted))', opacity: 0.4}}
                  contentStyle={{ 
                    backgroundColor: 'hsl(var(--card))', 
                    borderColor: 'hsl(var(--border))', 
                    color: 'hsl(var(--foreground))' 
                  }}
                  itemStyle={{ color: 'hsl(var(--foreground))' }}
               />
               <Bar dataKey="frequency" radius={[0, 4, 4, 0]}>
                 {data.map((entry, index) => (
                   <Cell key={`cell-${index}`} fill={`hsl(var(--primary))`}/>
                 ))}
               </Bar>
             </BarChart>
           </ResponsiveContainer>
        )}
      </div>
    </div>
  );
};

export default TopicCooccurrences;
