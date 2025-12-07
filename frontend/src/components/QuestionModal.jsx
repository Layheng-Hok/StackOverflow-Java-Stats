import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { X, User, MessageSquare } from 'lucide-react';

const QuestionModal = ({ isOpen, onClose, questionId }) => {
  const [details, setDetails] = useState(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (isOpen && questionId) {
      setLoading(true);
      axios.get(`/api/questions/${questionId}`)
        .then(res => setDetails(res.data))
        .catch(console.error)
        .finally(() => setLoading(false));
    }
  }, [isOpen, questionId]);

  if (!isOpen) return null;

  return (
    <div 
      className="fixed inset-0 z-[100] flex items-center justify-center bg-black/50 backdrop-blur-sm p-4"
      onClick={onClose}
    >
      <div 
        className="relative w-full max-w-4xl max-h-[90vh] bg-background rounded-lg shadow-lg border flex flex-col overflow-hidden animate-in fade-in zoom-in duration-200"
        onClick={(e) => e.stopPropagation()}
      >
        
        {loading ? (
          <div className="flex h-40 items-center justify-center relative p-6">
            <button 
              onClick={onClose}
              className="absolute right-4 top-4 p-2 rounded-full hover:bg-muted transition-colors"
            >
              <X size={20} />
            </button>
            Loading details...
          </div>
        ) : details ? (
          <>
            {/* Sticky Header */}
            <div className="p-6 pb-4 border-b bg-background shrink-0 flex justify-between items-start gap-4">
              <div className="flex-1 min-w-0">
                <h2 className="text-2xl font-bold text-primary leading-tight" dangerouslySetInnerHTML={{__html: details.title}} />
                <div className="flex gap-2 mt-3 flex-wrap">
                  {details.tags.map(tag => (
                    <span key={tag} className="px-2 py-1 bg-secondary text-secondary-foreground text-xs rounded-md">
                      {tag}
                    </span>
                  ))}
                </div>
              </div>
              
              <button 
                onClick={onClose}
                className="p-2 rounded-full hover:bg-muted transition-colors shrink-0 -mr-2 -mt-2"
              >
                <X size={20} />
              </button>
            </div>

            {/* Scrollable Body */}
            <div className="overflow-y-auto p-6 pt-4">
              <div className="prose dark:prose-invert max-w-none text-sm border-b pb-6">
                <div dangerouslySetInnerHTML={{ __html: details.body }} />
              </div>

              <div className="flex justify-between items-center text-sm text-muted-foreground mt-4 mb-6">
                 <div className="flex items-center gap-2">
                   <User size={16} />
                   <span>{details.owner.displayName} ({details.owner.reputation})</span>
                 </div>
                 <div>Score: {details.score} | Views: {details.viewCount}</div>
              </div>
              
              {details.answers && details.answers.length > 0 && (
                <div className="bg-muted/30 p-4 rounded-md">
                   <h3 className="font-semibold mb-4 flex items-center gap-2">
                      <MessageSquare size={16} /> Answers ({details.answers.length})
                   </h3>
                   <div className="space-y-4">
                     {details.answers.map(ans => (
                        <div 
                          key={ans.answerId} 
                          className={`p-4 rounded border ${
                            ans.isAccepted 
                              ? 'border-green-500 ring-1 ring-green-500 bg-green-500/10' 
                              : 'bg-card'
                          }`}
                        >
                           <div className="prose dark:prose-invert text-sm max-w-none" dangerouslySetInnerHTML={{__html: ans.body}} />
                           <div className="mt-2 text-xs text-muted-foreground text-right">
                              Answered by {ans.owner.displayName} {ans.isAccepted && '(Accepted)'}
                           </div>
                        </div>
                     ))}
                   </div>
                </div>
              )}
            </div>
          </>
        ) : (
          <div className="p-6 relative">
            <button 
              onClick={onClose}
              className="absolute right-4 top-4 p-2 rounded-full hover:bg-muted transition-colors"
            >
              <X size={20} />
            </button>
            <div>Failed to load details.</div>
          </div>
        )}
      </div>
    </div>
  );
};

export default QuestionModal;
